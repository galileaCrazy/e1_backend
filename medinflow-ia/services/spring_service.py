import httpx
import os

SPRING_URL = os.getenv("SPRING_BOOT_URL", "http://localhost:8080")

async def obtener_medicos(organizacion_id: str) -> list:
    try:
        async with httpx.AsyncClient() as client:
            response = await client.get(
                f"{SPRING_URL}/api/medicos/organizacion/{organizacion_id}"
            )
            if response.status_code == 200:
                return response.json()
            return []
    except Exception:
        return []

async def obtener_horarios(medico_id: str) -> list:
    try:
        async with httpx.AsyncClient() as client:
            response = await client.get(
                f"{SPRING_URL}/api/horarios/medico/{medico_id}"
            )
            if response.status_code == 200:
                return response.json()
            return []
    except Exception:
        return []
