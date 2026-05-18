ORG_ID = "20352770-80b9-403b-9a3d-e55e02e98edd"

REGISTRO_PAYLOAD = {
    "nombre": "Test Paciente",
    "email": "testpaciente@clinic.com",
    "password": "Password123",
    "rol": "PACIENTE",
    "organizacionId": ORG_ID,
}
LOGIN_PAYLOAD = {"email": "testpaciente@clinic.com", "password": "Password123"}


def test_registro_exitoso(client, monkeypatch):
    async def mock_registro(data):
        return 201, {"id": "abc-123", "email": data["email"], "rol": data["rol"]}

    monkeypatch.setattr("services.auth_service.call_spring_registro", mock_registro)
    resp = client.post("/api/auth/registro", json=REGISTRO_PAYLOAD)
    assert resp.status_code == 201


def test_login_exitoso(client, monkeypatch):
    async def mock_login(data):
        return 200, {"token": "eyJhbGciOiJIUzI1NiJ9.fake.token", "tipo": "Bearer"}

    monkeypatch.setattr("services.auth_service.call_spring_login", mock_login)
    resp = client.post("/api/auth/login", json=LOGIN_PAYLOAD)
    assert resp.status_code == 200
    assert "token" in resp.json()


def test_sin_token(client):
    resp = client.get(f"/api/ia/disponibilidad/{ORG_ID}")
    assert resp.status_code == 401


def test_token_invalido(client):
    resp = client.get(
        f"/api/ia/disponibilidad/{ORG_ID}",
        headers={"Authorization": "Bearer esto.no.es.un.token.valido"},
    )
    assert resp.status_code == 401


def test_rol_insuficiente(client, token_paciente):
    resp = client.get(
        f"/api/ia/disponibilidad/{ORG_ID}",
        headers={"Authorization": f"Bearer {token_paciente}"},
    )
    assert resp.status_code == 403
