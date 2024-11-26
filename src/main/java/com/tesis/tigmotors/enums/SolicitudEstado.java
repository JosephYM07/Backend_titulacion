package com.tesis.tigmotors.enums;

public enum SolicitudEstado {
    PENDIENTE, // Cuando la solicitud es creada y está a la espera de revisión
    ACEPTADO, // Cuando el administrador acepta la solicitud
    COTIZACION_ACEPTADA, // Cuando el usuario acepta la cotización
    SOLICITUD_RECHAZADA, // Cuando el administrador rechaza la solicitud
    RECHAZO_COTIZACION_USUARIO; // Cuando el usuario rechaza la cotización

    /**
     * Verifica si un estado dado es válido en este enum.
     *
     * @param estado Estado a validar.
     * @return true si el estado es válido, de lo contrario false.
     */
    public static boolean isValid(String estado) {
        for (SolicitudEstado e : values()) {
            if (e.name().equalsIgnoreCase(estado)) {
                return true;
            }
        }
        return false;
    }
}