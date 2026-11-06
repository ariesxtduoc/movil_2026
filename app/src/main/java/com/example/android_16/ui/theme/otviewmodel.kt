package com.example.android_16.ui.theme

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.android_16.EstadoOt
import com.example.android_16.OrdenTrabajo
import com.example.android_16.Prioridad

class OtViewModel : ViewModel() {
    val ordenes = mutableStateListOf(
        OrdenTrabajo(
            id = "OT-101",
            cliente = "Supermercado Lider - Centro",
            direccion = "Av. O'Higgins 456",
            tipoServicio = "Mantenimiento Preventivo Aire",
            equipo = "Unidad Interior Split 24000 BTU",
            esEquipoExterior = false,
            prioridad = Prioridad.ALTA,
            estado = EstadoOt.PENDIENTE
        ),
        OrdenTrabajo(
            id = "OT-201",
            cliente = "Mall Plaza - Ripley",
            direccion = "Autopista Concepción 9000",
            tipoServicio = "Reparación Fuga Refrigerante",
            equipo = "Compresor Azotea Central 02",
            esEquipoExterior = true,
            prioridad = Prioridad.ALTA,
            estado = EstadoOt.PENDIENTE
        ),
        OrdenTrabajo(
            id = "OT-305",
            cliente = "Jumbo - San Pedro",
            direccion = "Av. Pedro Aguirre Cerda 1020",
            tipoServicio = "Revisión Sistema Frío Comercial",
            equipo = "Vitrina Refrigerada Quesos",
            esEquipoExterior = false,
            prioridad = Prioridad.MEDIA,
            estado = EstadoOt.COMPLETADO,
            checkInHora = "09:15 AM",
            checkOutHora = "10:45 AM",
            diagnostico = "Filtro obstruido y baja presión de gas.",
            actividades = "Limpieza general de condensador y recarga parcial de refrigerante.",
            fotoEvidenciaUrl = "evidencia_305.jpg",
            firmaCliente = true
        )
    )

    fun registrarCheckIn(id: String, hora: String) {
        val index = ordenes.indexOfFirst { it.id == id }
        if (index != -1) {
            ordenes[index] = ordenes[index].copy(
                estado = EstadoOt.EN_PROCESO,
                checkInHora = hora
            )
        }
    }

    fun guardarOActualizarOrden(
        id: String,
        diagnostico: String,
        actividades: String,
        fotoUrl: String?,
        checkOutHora: String
    ) {
        val index = ordenes.indexOfFirst { it.id == id }
        if (index != -1) {
            ordenes[index] = ordenes[index].copy(
                diagnostico = diagnostico,
                actividades = actividades,
                fotoEvidenciaUrl = fotoUrl ?: ordenes[index].fotoEvidenciaUrl,
                checkOutHora = checkOutHora,
                estado = EstadoOt.COMPLETADO
            )
        }
    }
}