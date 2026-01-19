import os
import sys

ROOT_REL = os.path.join("backend", "dfbs-app", "src")
MAIN_JAVA_REL = os.path.join(ROOT_REL, "main", "java", "com", "dfbs", "app")
TEST_JAVA_REL = os.path.join(ROOT_REL, "test", "java", "com", "dfbs", "app")

def ensure_dir(path: str) -> None:
    os.makedirs(path, exist_ok=True)

def write_if_not_exists(path: str, content: str) -> None:
    if os.path.exists(path):
        print(f"SKIP (exists): {path}")
        return
    ensure_dir(os.path.dirname(path))
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"CREATE: {path}")

def validate_args(module_key: str, module_name: str) -> None:
    if not module_key.islower():
        raise ValueError("module_key must be lowercase, e.g. shipment")
    if not module_name[:1].isalpha() or not module_name[0].isupper():
        raise ValueError("ModuleName must be PascalCase, e.g. Shipment")
    if not module_name.replace("_", "").replace("-", "").isalnum():
        raise ValueError("ModuleName must be alphanumeric (no spaces)")

def main() -> int:
    if len(sys.argv) != 3:
        print("Usage: python tools/new_module.py <module_key> <ModuleName>")
        return 1

    module_key = sys.argv[1].strip()
    module_name = sys.argv[2].strip()

    try:
        validate_args(module_key, module_name)
    except Exception as e:
        print(f"ERROR: {e}")
        return 1

    interfaces_pkg_dir = os.path.join(MAIN_JAVA_REL, "interfaces", module_key)
    application_pkg_dir = os.path.join(MAIN_JAVA_REL, "application", module_key)
    modules_pkg_dir = os.path.join(MAIN_JAVA_REL, "modules", module_key)
    test_pkg_dir = os.path.join(TEST_JAVA_REL, "interfaces", module_key)

    # 1) Controller
    controller_path = os.path.join(interfaces_pkg_dir, f"{module_name}Controller.java")
    controller_code = f"""package com.dfbs.app.interfaces.{module_key};

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/{module_key}")
public class {module_name}Controller {{

    // TODO: add endpoints later (no business assumptions here)
}}
"""
    # 2) Service
    service_path = os.path.join(application_pkg_dir, f"{module_name}Service.java")
    service_code = f"""package com.dfbs.app.application.{module_key};

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class {module_name}Service {{

    @Transactional
    public void placeholder() {{
        // TODO: implement later
    }}
}}
"""
    # 3) Entity
    entity_path = os.path.join(modules_pkg_dir, f"{module_name}Entity.java")
    entity_code = f"""package com.dfbs.app.modules.{module_key};

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "{module_key}_placeholder")
public class {module_name}Entity {{

    @Id
    private UUID id;

    public UUID getId() {{
        return id;
    }}

    public void setId(UUID id) {{
        this.id = id;
    }}
}}
"""
    # 4) Repo
    repo_path = os.path.join(modules_pkg_dir, f"{module_name}Repo.java")
    repo_code = f"""package com.dfbs.app.modules.{module_key};

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface {module_name}Repo extends JpaRepository<{module_name}Entity, UUID> {{
}}
"""
    # 5) Test skeleton
    test_path = os.path.join(test_pkg_dir, f"{module_name}SmokeTest.java")
    test_code = f"""package com.dfbs.app.interfaces.{module_key};

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class {module_name}SmokeTest {{

    @Test
    void contextLoads() {{
        // empty - just make sure Spring context can start
    }}
}}
"""

    write_if_not_exists(controller_path, controller_code)
    write_if_not_exists(service_path, service_code)
    write_if_not_exists(entity_path, entity_code)
    write_if_not_exists(repo_path, repo_code)
    write_if_not_exists(test_path, test_code)

    print("DONE")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
