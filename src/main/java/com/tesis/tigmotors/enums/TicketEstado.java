package com.tesis.tigmotors.enums;

public enum TicketEstado {
    PENDIENTE,
    APROBADO,
    RECHAZADO;

    public static boolean isValidEstado(String estado) {
        for (TicketEstado ticketEstado : TicketEstado.values()) {
            if (ticketEstado.name().equalsIgnoreCase(estado)) {
                return true;
            }
        }
        return false;
    }
}