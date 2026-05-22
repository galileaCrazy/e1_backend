import os
import base64
import binascii
import jwt
from fastapi import Depends, HTTPException
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials

_bearer = HTTPBearer(auto_error=False)


def _jwt_secret():
    secret = os.getenv("JWT_SECRET", "changeme")
    try:
        return base64.b64decode(secret, validate=True)
    except (binascii.Error, ValueError):
        return secret


def get_current_user(
    creds: HTTPAuthorizationCredentials = Depends(_bearer),
) -> dict:
    if creds is None:
        raise HTTPException(status_code=401, detail="Token requerido")
    try:
        payload = jwt.decode(creds.credentials, _jwt_secret(), algorithms=["HS256"])
        return payload
    except jwt.PyJWTError:
        raise HTTPException(status_code=401, detail="Token inválido")


def require_roles(*roles: str):
    def _dep(user: dict = Depends(get_current_user)) -> dict:
        if user.get("rol") not in roles:
            raise HTTPException(status_code=403, detail="Acceso denegado")
        return user

    return _dep
