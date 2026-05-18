import services.auth_service as auth_service
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel

router = APIRouter(prefix="/api/auth", tags=["Auth"])


class RegistroRequest(BaseModel):
    nombre: str
    email: str
    password: str
    rol: str
    organizacionId: str


class LoginRequest(BaseModel):
    email: str
    password: str


@router.post("/registro", status_code=201)
async def registro(req: RegistroRequest):
    status, body = await auth_service.call_spring_registro(req.model_dump())
    if status != 201:
        raise HTTPException(status_code=status, detail=str(body))
    return body


@router.post("/login")
async def login(req: LoginRequest):
    status, body = await auth_service.call_spring_login(req.model_dump())
    if status != 200:
        raise HTTPException(status_code=status, detail=str(body))
    return body
