import httpx
import os


async def call_spring_registro(data: dict) -> tuple:
    url = os.getenv("SPRING_BOOT_URL", "http://localhost:8080")
    async with httpx.AsyncClient() as client:
        resp = await client.post(f"{url}/api/auth/registro", json=data)
        return resp.status_code, resp.json()


async def call_spring_login(data: dict) -> tuple:
    url = os.getenv("SPRING_BOOT_URL", "http://localhost:8080")
    async with httpx.AsyncClient() as client:
        resp = await client.post(f"{url}/api/auth/login", json=data)
        return resp.status_code, resp.json()
