package com.microservice.clientes.Clientes_microservice_curse.Clients;

import com.microservice.clientes.Clientes_microservice_curse.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/api/usuarios")
public interface UserServiceClient {
    @GetMapping("/{id}")
    UserDTO getUsuarioById(@PathVariable("id") Long id);
}
