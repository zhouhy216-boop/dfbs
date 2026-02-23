package com.dfbs.app.application.dicttype;

import com.dfbs.app.modules.dicttype.DictItemRepo;
import com.dfbs.app.modules.dicttype.DictTypeEntity;
import com.dfbs.app.modules.dicttype.DictTypeRepo;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class DictTypeService {

    private static final String DICT_TYPE_CODE_EXISTS = "DICT_TYPE_CODE_EXISTS";
    private static final int TYPE_CODE_MAX = 64;
    private static final int TYPE_NAME_MAX = 128;
    private static final int DESCRIPTION_MAX = 512;
    /** type_code: letters, numbers, underscore, hyphen only */
    private static final Pattern TYPE_CODE_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");

    private final DictTypeRepo repo;
    private final DictItemRepo itemRepo;

    public DictTypeService(DictTypeRepo repo, DictItemRepo itemRepo) {
        this.repo = repo;
        this.itemRepo = itemRepo;
    }

    public record ListResult(List<DictTypeEntity> items, long total) {}

    public ListResult list(String q, Boolean enabled, int page, int pageSize) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, Math.min(100, pageSize)), Sort.by("id"));
        Specification<DictTypeEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                String term = "%" + q.trim() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("typeCode")), term.toLowerCase()),
                        cb.like(cb.lower(root.get("typeName")), term.toLowerCase())
                ));
            }
            if (enabled != null) {
                predicates.add(cb.equal(root.get("enabled"), enabled));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        var springPage = repo.findAll(spec, pageable);
        return new ListResult(springPage.getContent(), springPage.getTotalElements());
    }

    private void validateTypeCode(String typeCode) {
        if (typeCode == null || typeCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "type_code 不能为空");
        }
        if (typeCode.length() > TYPE_CODE_MAX) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "type_code 长度不能超过 " + TYPE_CODE_MAX);
        }
        if (!TYPE_CODE_PATTERN.matcher(typeCode).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "type_code 仅允许字母、数字、下划线、连字符");
        }
    }

    private void validateTypeName(String typeName) {
        if (typeName == null || typeName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "type_name 不能为空");
        }
        if (typeName.length() > TYPE_NAME_MAX) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "type_name 长度不能超过 " + TYPE_NAME_MAX);
        }
    }

    private void validateDescription(String description) {
        if (description != null && description.length() > DESCRIPTION_MAX) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "description 长度不能超过 " + DESCRIPTION_MAX);
        }
    }

    @Transactional
    public DictTypeEntity create(String typeCode, String typeName, String description, Boolean enabled) {
        validateTypeCode(typeCode);
        validateTypeName(typeName);
        validateDescription(description);
        if (repo.existsByTypeCode(typeCode.trim())) {
            throw new DictTypeCodeExistsException();
        }
        DictTypeEntity e = new DictTypeEntity();
        e.setTypeCode(typeCode.trim());
        e.setTypeName(typeName.trim());
        e.setDescription(description != null ? description.trim() : null);
        e.setEnabled(enabled != null ? enabled : true);
        e.setCreatedAt(Instant.now());
        e.setUpdatedAt(Instant.now());
        return repo.save(e);
    }

    @Transactional
    public DictTypeEntity update(Long id, String typeName, String description, Boolean enabled) {
        DictTypeEntity e = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "字典类型不存在"));
        if (typeName != null) {
            validateTypeName(typeName);
            e.setTypeName(typeName.trim());
        }
        if (description != null) {
            validateDescription(description);
            e.setDescription(description.trim());
        }
        if (enabled != null) {
            e.setEnabled(enabled);
        }
        e.setUpdatedAt(Instant.now());
        return repo.save(e);
    }

    @Transactional
    public DictTypeEntity setEnabled(Long id, boolean enabled) {
        DictTypeEntity e = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "字典类型不存在"));
        e.setEnabled(enabled);
        e.setUpdatedAt(Instant.now());
        return repo.save(e);
    }

    /**
     * Delete type only when it has zero dict_items. Otherwise throws DictTypeDeleteNotAllowedUsedException.
     */
    @Transactional
    public void deleteById(Long id) {
        DictTypeEntity e = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "字典类型不存在"));
        if (itemRepo.countByTypeId(id) > 0) {
            throw new DictTypeDeleteNotAllowedUsedException();
        }
        repo.delete(e);
    }

    /** Thrown when create is called with duplicate type_code; controller maps to 400 + DICT_TYPE_CODE_EXISTS */
    @SuppressWarnings("serial")
    public static class DictTypeCodeExistsException extends RuntimeException {}

    /** Thrown when delete is called but type has dict_items; controller maps to 400 + DICT_TYPE_DELETE_NOT_ALLOWED_USED */
    @SuppressWarnings("serial")
    public static class DictTypeDeleteNotAllowedUsedException extends RuntimeException {}
}
