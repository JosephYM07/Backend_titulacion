package com.tesis.tigmotors.enums;

public enum TicketEstado {
    PENDIENTE,
    APROBADO,
    RECHAZADO;

    // Puedes agregar métodos de validación como el isValidEstado
    public static boolean isValidEstado(String estado) {
        for (TicketEstado ticketEstado : TicketEstado.values()) {
            if (ticketEstado.name().equalsIgnoreCase(estado)) {
                return true;
            }
        }
        return false;
    }
}