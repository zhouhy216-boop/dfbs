package com.dfbs.app.modules.platformorg;

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
public class PlatformOrgCustomerId implements Serializable {

    private Long orgId;
    private Long customerId;
}
