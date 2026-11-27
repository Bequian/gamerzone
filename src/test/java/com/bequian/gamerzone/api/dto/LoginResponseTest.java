package com.bequian.gamerzone.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class LoginResponseTest {

    @Test
    void guardaTokenYRoles() {
        LoginResponse r = new LoginResponse("abc.def.ghi", "Bearer", 3600, "admin", List.of("ROLE_ADMIN"));
        assertThat(r.token()).isEqualTo("abc.def.ghi");
        assertThat(r.tipo()).isEqualTo("Bearer");
        assertThat(r.expiraEnSegundos()).isEqualTo(3600);
        assertThat(r.username()).isEqualTo("admin");
        assertThat(r.roles()).containsExactly("ROLE_ADMIN");
    }
}
