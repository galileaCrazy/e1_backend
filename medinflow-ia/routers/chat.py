from fastapi import APIRouter, Depends
from pydantic import BaseModel
import services.groq_service as groq_service
from services.spring_service import obtener_medicos
from security.jwt_dep import get_current_user, require_roles

router = APIRouter(prefix="/api/ia", tags=["IA - Chat"])


class MensajeRequest(BaseModel):
    mensaje: str
    historial: list = []
    organizacion_id: str = None


class MensajeResponse(BaseModel):
    respuesta: str
    fuente: str = "llama-3.3-70b-versatile"


@router.post("/chat", response_model=MensajeResponse)
def chat(request: MensajeRequest, user: dict = Depends(get_current_user)):
    respuesta = groq_service.obtener_respuesta(request.mensaje, request.historial)
    return MensajeResponse(respuesta=respuesta)


@router.get("/disponibilidad/{organizacion_id}")
async def disponibilidad(
    organizacion_id: str,
    user: dict = Depends(require_roles("ADMIN", "MEDICO")),
):
    medicos = await obtener_medicos(organizacion_id)
    return {
        "organizacion_id": organizacion_id,
        "medicos_disponibles": medicos,
        "total": len(medicos),
    }


@router.get("/salud")
def salud():
    return {"status": "ok", "servicio": "MedInFlow IA"}
