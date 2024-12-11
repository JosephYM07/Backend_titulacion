package com.tesis.tigmotors.enums;

public enum SolicitudEstado {
    PENDIENTE, // Cuando la solicitud es creada y está a la espera de revisión
    ACEPTADO, // Cuando el administrador acepta la solicitud
    COTIZACION_ACEPTADA, // Cuando el usuario acepta la cotización
    SOLICITUD_RECHAZADA, // Cuando el administrador rechaza la solicitud
    RECHAZO_COTIZACION_USUARIO,// Cuando el usuario rechaza la cotización
    ALTA, //Prioridad de solicitud
    MEDIA,//Prioridad de solicitud
    BAJA,
    PAGO_PENDIENTE; //Prioridad de solicitud

    /**
     * Normaliza y valida la prioridad.
     *
     * @param estado Estado ingresado.
     * @return El valor del enum correspondiente.
     * @throws IllegalArgumentException Si el estado no es válido.
     */
    public static SolicitudEstado fromString(String estado) {
        for (SolicitudEstado e : SolicitudEstado.values()) {
            if (e.name().equalsIgnoreCase(estado)) {
                return e;
            }
        }
        throw new IllegalArgumentException("La prioridad proporcionada no es válida. Use ALTA, MEDIO o BAJA.");
    }

}