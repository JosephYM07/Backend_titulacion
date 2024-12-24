package com.tesis.tigmotors.enums;

import java.util.EnumSet;
import java.util.Set;

/**
 * Enum que representa los posibles estados de un ticket y las prioridades asociadas.
 */
public enum TicketEstado {
    TRABAJO_PENDIENTE,
    TRABAJO_EN_PROGRESO,
    TRABAJO_TERMINADO,
    ALTA,
    MEDIA,
    BAJA;

    private static final Set<TicketEstado> ESTADOS_TRABAJO = EnumSet.of(TRABAJO_PENDIENTE, TRABAJO_EN_PROGRESO, TRABAJO_TERMINADO);
    private static final Set<TicketEstado> PRIORIDADES = EnumSet.of(ALTA, MEDIA, BAJA);

    /**
     * Método centralizado para validar y convertir cadenas a TicketEstado.
     *
     * @param estado  Estado ingresado.
     * @param validSet Conjunto de estados válidos.
     * @param errorMsg Mensaje de error si el estado no es válido.
     * @return El valor del enum si es válido.
     */
    private static TicketEstado fromString(String estado, Set<TicketEstado> validSet, String errorMsg) {
        for (TicketEstado e : validSet) {
            if (e.name().equalsIgnoreCase(estado)) {
                return e;
            }
        }
        throw new IllegalArgumentException(errorMsg);
    }

    /**
     * Convierte y valida un estado para trabajos.
     *
     * @param estado Estado ingresado.
     * @return El valor del enum si es válido.
     */
    public static TicketEstado fromTrabajoString(String estado) {
        return fromString(estado, ESTADOS_TRABAJO, "El estado proporcionado no es válido. Estados válidos: TRABAJO_PENDIENTE, TRABAJO_EN_PROGRESO, TRABAJO_TERMINADO.");
    }

    /**
     * Convierte y valida un estado para prioridades.
     *
     * @param estado Estado ingresado.
     * @return El valor del enum si es válido.
     */
    public static TicketEstado fromPrioridadString(String estado) {
        return fromString(estado, PRIORIDADES, "La prioridad proporcionada no es válida. Estados válidos: ALTA, MEDIA, BAJA.");
    }
}
