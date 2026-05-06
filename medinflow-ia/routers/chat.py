from fastapi import APIRouter
from pydantic import BaseModel
from services.groq_service import obtener_respuesta
from services.spring_service import obtener_medicos

router = APIRouter(prefix="/api/ia", tags=["IA - Chat"])

class MensajeRequest(BaseModel):
    mensaje: str
    historial: list = []
    organizacion_id: str = None

class MensajeResponse(BaseModel):
    respuesta: str
    fuente: str = "llama-3.3-70b-versatile"

@router.post("/chat", response_model=MensajeResponse)
def chat(request: MensajeRequest):
    respuesta = obtener_respuesta(request.mensaje, request.historial)
    return MensajeResponse(respuesta=respuesta)

@router.get("/disponibilidad/{organizacion_id}")
async def disponibilidad(organizacion_id: str):
    medicos = await obtener_medicos(organizacion_id)
    return {
        "organizacion_id": organizacion_id,
        "medicos_disponibles": medicos,
        "total": len(medicos)
    }

@router.get("/salud")
def salud():
    return {"status": "ok", "servicio": "MedInFlow IA"}
