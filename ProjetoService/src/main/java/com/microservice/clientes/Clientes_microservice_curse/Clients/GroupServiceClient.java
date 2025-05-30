package com.microservice.clientes.Clientes_microservice_curse.Clients;

import com.microservice.clientes.Clientes_microservice_curse.dto.GroupDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "group-service", path = "/api/grupos")
public interface GroupServiceClient {
    @GetMapping("/{id}")
    GroupDTO getGrupoById(@PathVariable("id") Long id);
}
