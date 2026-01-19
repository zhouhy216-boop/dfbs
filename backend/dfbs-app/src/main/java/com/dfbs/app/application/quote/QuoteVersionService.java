package com.dfbs.app.application.quote;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dfbs.app.modules.quote.QuoteVersionRepo;
import com.dfbs.app.modules.quote.QuoteVersionEntity;

@Service
public class QuoteVersionService {

    private final QuoteVersionRepo repo;

    public QuoteVersionService(QuoteVersionRepo repo) {
        this.repo = repo;
    }

    /**
     * 封板目标：把“事务 + 同一 quoteNo 串行 + 先 deactivate 再 activate”的语义固定在 Service，
     * Controller 只做 HTTP 入参/出参。
     *
     * 注意：这里不引入任何新业务逻辑；请把你现有 Controller 中 activate 方法里
     * “事务内的完整实现”原封不动剪切到下面 TODO 区域。
     */
    @Transactional
    public void activate(String quoteNo, int versionNo) {

        // 同一 quoteNo 串行（事务内锁）
        repo.lockQuoteNo(quoteNo);

        QuoteVersionEntity target = repo.findByQuoteNoAndVersionNo(quoteNo, versionNo)
            .orElseThrow(() -> new IllegalArgumentException(
                "quote version not found: quoteNo=" + quoteNo + ", versionNo=" + versionNo
            ));

        // 先清空当前 active，再激活目标版本（不新增记录）
        repo.deactivateAllActiveByQuoteNo(quoteNo);
        repo.activateById(target.getId());

        // Service 不返回任何值；Controller 负责 HTTP 层返回
    }
}
