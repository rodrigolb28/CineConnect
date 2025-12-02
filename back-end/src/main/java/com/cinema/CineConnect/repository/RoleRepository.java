package com.cinema.CineConnect.repository;

import com.cinema.CineConnect.model.DTO.RoleRecord;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RoleRepository {
    private final JdbcClient jdbcClient;

    public RoleRepository(JdbcClient jdbcClient) {

        this.jdbcClient = jdbcClient;
    }

    public Optional<RoleRecord> findRoleByName(String name) {
        return jdbcClient.sql("SELECT * FROM roles WHERE name= :name")
                .params("name", name)
                .query(RoleRecord.class)
                .optional();
    }

    public Optional<Integer> findRoleID(String roleName) {
        return jdbcClient.sql("SELECT id FROM roles WHERE name=:roleName")
                .param("roleName", roleName)
                .query(Integer.class)
                .optional();
    }
}
