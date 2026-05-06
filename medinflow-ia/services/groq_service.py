import os
from groq import Groq
from dotenv import load_dotenv

load_dotenv()  # ← aquí, en este archivo

client = Groq(api_key=os.getenv("GROQ_API_KEY"))

SYSTEM_PROMPT = """
Eres un asistente virtual de MedInFlow, un sistema de administración de clínicas.
Tu función es ayudar a los pacientes a:
- Agendar citas médicas
- Consultar disponibilidad de médicos
- Obtener información sobre consultorios y especialidades
- Responder preguntas frecuentes sobre la clínica

Responde siempre en español, de forma amable y profesional.
Si el paciente quiere agendar una cita, pídele: nombre completo, especialidad deseada y fecha preferida.
No inventes información médica ni des diagnósticos.
"""

def obtener_respuesta(mensaje: str, historial: list = []) -> str:
    mensajes = [{"role": "system", "content": SYSTEM_PROMPT}]
    mensajes.extend(historial)
    mensajes.append({"role": "user", "content": mensaje})

    respuesta = client.chat.completions.create(
        model="llama-3.3-70b-versatile",
        messages=mensajes,
        temperature=0.7,
        max_tokens=500
    )

    return respuesta.choices[0].message.content