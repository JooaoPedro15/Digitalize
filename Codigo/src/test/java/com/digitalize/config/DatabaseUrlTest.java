package com.digitalize.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatabaseUrlTest {

    @Test
    void converteUrlDoNeonParaJdbc() {
        DatabaseUrl url = DatabaseUrl.parse(
                "postgresql://neondb_owner:s3nh%40forte@ep-exemplo.us-east-2.aws.neon.tech/neondb?sslmode=require");

        assertThat(url.jdbcUrl())
                .isEqualTo("jdbc:postgresql://ep-exemplo.us-east-2.aws.neon.tech/neondb?sslmode=require");
        assertThat(url.usuario()).isEqualTo("neondb_owner");
        assertThat(url.senha()).isEqualTo("s3nh@forte");
    }

    @Test
    void mantemPortaQuandoInformada() {
        DatabaseUrl url = DatabaseUrl.parse("postgres://user:pass@localhost:5499/digitalize");

        assertThat(url.jdbcUrl()).isEqualTo("jdbc:postgresql://localhost:5499/digitalize");
    }

    @Test
    void aceitaUrlJdbcSemAlterar() {
        DatabaseUrl url = DatabaseUrl.parse("jdbc:postgresql://localhost/digitalize?user=a&password=b");

        assertThat(url.jdbcUrl()).isEqualTo("jdbc:postgresql://localhost/digitalize?user=a&password=b");
        assertThat(url.usuario()).isNull();
    }

    @Test
    void recusaOutrosFormatos() {
        assertThatThrownBy(() -> DatabaseUrl.parse("mysql://localhost/db"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
