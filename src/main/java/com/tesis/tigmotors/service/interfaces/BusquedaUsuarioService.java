package com.tesis.tigmotors.service.interfaces;

import com.tesis.tigmotors.dto.Request.UserRequestDTO;
import org.springframework.http.ResponseEntity;

public interface BusquedaUsuarioService {
    ResponseEntity<?> buscarUsuario(UserRequestDTO request);
}
