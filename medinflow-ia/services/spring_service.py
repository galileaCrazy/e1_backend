import httpx
import logging
import os

SPRING_URL = os.getenv("SPRING_BOOT_URL", "http://localhost:8080")
logger = logging.getLogger(__name__)

# ── Async (usadas desde routers) ────────────────────────────────────────────────

async def obtener_medicos(organizacion_id: str) -> list:
    try:
        async with httpx.AsyncClient() as client:
            response = await client.get(
                f"{SPRING_URL}/api/medicos/organizacion/{organizacion_id}"
            )
            return response.json() if response.status_code == 200 else []
    except Exception:
        return []


async def obtener_horarios(medico_id: str) -> list:
    try:
        async with httpx.AsyncClient() as client:
            response = await client.get(
                f"{SPRING_URL}/api/horarios/medico/{medico_id}"
            )
            return response.json() if response.status_code == 200 else []
    except Exception:
        return []


# ── Síncronas (usadas desde tool calling en groq_service) ───────────────────────

def buscar_medicos_sync(especialidad: str, org_id: str, auth_header: str) -> list:
    try:
        headers = {"Authorization": auth_header} if auth_header else {}
        with httpx.Client(timeout=10.0) as client:
            response = client.get(
                f"{SPRING_URL}/api/ia/medicos/{org_id}",
                headers=headers
            )
            if response.status_code != 200:
                return []
            medicos = response.json()
            esp = especialidad.lower()
            filtrados = [m for m in medicos if esp in (m.get("especialidad") or "").lower()]
            return filtrados if filtrados else medicos
    except Exception as e:
        logger.error("Error buscando médicos: %s", e)
        return []


def consultar_horarios_sync(medico_id: str, fecha: str, auth_header: str) -> list:
    """Devuelve los slots libres de un médico para una fecha (YYYY-MM-DD)."""
    try:
        headers = {"Authorization": auth_header} if auth_header else {}
        with httpx.Client(timeout=10.0) as client:
            response = client.get(
                f"{SPRING_URL}/api/ia/horarios/{medico_id}",
                params={"fecha": fecha},
                headers=headers
            )
            if response.status_code != 200:
                return []
            return response.json().get("slots", [])
    except Exception as e:
        logger.error("Error consultando horarios: %s", e)
        return []


def crear_cita_sync(datos: dict, auth_header: str) -> dict:
    try:
        headers = {"Authorization": auth_header} if auth_header else {}
        with httpx.Client(timeout=15.0) as client:
            response = client.post(
                f"{SPRING_URL}/api/ia/agendar",
                json=datos,
                headers=headers
            )
            if response.status_code in (200, 201):
                return response.json()
            return {"error": f"Error {response.status_code}: {response.text}"}
    except Exception as e:
        logger.error("Error creando cita: %s", e)
        return {"error": str(e)}
