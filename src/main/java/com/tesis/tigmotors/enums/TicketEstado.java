package com.tesis.tigmotors.enums;

public enum TicketEstado {
    PENDIENTE,
    APROBADO,
    RECHAZADO;

    public static boolean isValidEstado(String estado) {
        for (TicketEstado te : TicketEstado.values()) {
            if (te.name().equalsIgnoreCase(estado)) {
                return true;
            }
        }
        return false;
    }
}
