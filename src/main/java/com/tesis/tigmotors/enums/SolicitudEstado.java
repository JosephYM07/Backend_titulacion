package com.tesis.tigmotors.enums;

public enum SolicitudEstado {
    PENDIENTE, // Cuando la solicitud es creada y está a la espera de revisión
    ACEPTADO, // Cuando el administrador acepta la solicitud
    COTIZACION_ACEPTADA, // Cuando el usuario acepta la cotización
    SOLICITUD_RECHAZADA, // Cuando el administrador rechaza la solicitud
    RECHAZO_COTIZACION_USUARIO; // Cuando el usuario rechaza la cotización

    /**
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
    /**
     * Verifica si la transición de un estado a otro es válida.
     *
     * @param currentState Estado actual.
     * @param nextState Estado al que se desea transitar.
     * @return true si la transición es válida, de lo contrario false.
     */
    public static boolean isValidTransition(SolicitudEstado currentState, SolicitudEstado nextState) {
        if (currentState == PENDIENTE) {
            return nextState == ACEPTADO || nextState == SOLICITUD_RECHAZADA;
        } else if (currentState == ACEPTADO) {
            return nextState == COTIZACION_ACEPTADA || nextState == SOLICITUD_RECHAZADA;
        } else if (currentState == COTIZACION_ACEPTADA) {
            return nextState == RECHAZO_COTIZACION_USUARIO;
        } else if (currentState == RECHAZO_COTIZACION_USUARIO) {
            return false; // No se puede volver a modificar una cotización rechazada por el usuario
        } else if (currentState == SOLICITUD_RECHAZADA) {
            return false; // No se puede modificar una solicitud rechazada
        }
        return false; // Para otros estados, no es válida la transición
    }
}