import jwt
import pytest
from datetime import datetime, timedelta, timezone
from fastapi.testclient import TestClient
from main import app

TEST_SECRET = "test-secret-key"


def make_token(rol: str, email: str = "test@clinic.com") -> str:
    payload = {
        "sub": email,
        "userId": "11111111-0000-0000-0000-000000000001",
        "organizacionId": "22222222-0000-0000-0000-000000000001",
        "rol": rol,
        "exp": datetime.now(timezone.utc) + timedelta(hours=1),
    }
    return jwt.encode(payload, TEST_SECRET, algorithm="HS256")


@pytest.fixture(scope="session")
def client():
    with TestClient(app) as c:
        yield c


@pytest.fixture
def token_medico():
    return make_token("MEDICO")


@pytest.fixture
def token_paciente():
    return make_token("PACIENTE")


@pytest.fixture
def token_admin():
    return make_token("ADMIN")
