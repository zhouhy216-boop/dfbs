package com.dfbs.app.modules.bizperm;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BizPermUserOpScopeId implements Serializable {

    private Long userId;
    private String permissionKey;
}
