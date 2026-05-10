def test_chat_responde(client, monkeypatch, token_medico):
    monkeypatch.setattr(
        "services.groq_service.obtener_respuesta",
        lambda mensaje, historial=[]: "Hola, ¿en qué puedo ayudarte?",
    )
    resp = client.post(
        "/api/ia/chat",
        json={"mensaje": "Hola"},
        headers={"Authorization": f"Bearer {token_medico}"},
    )
    assert resp.status_code == 200
    assert resp.json()["respuesta"] != ""


def test_chat_sin_mensaje(client, token_medico):
    resp = client.post(
        "/api/ia/chat",
        json={},
        headers={"Authorization": f"Bearer {token_medico}"},
    )
    assert resp.status_code == 422
