import sys
import os

sys.path.insert(0, os.path.dirname(__file__))

os.environ.setdefault("JWT_SECRET", "changeme")
os.environ.setdefault("GROQ_API_KEY", "test-fake-groq-key")
os.environ.setdefault("SPRING_BOOT_URL", "http://spring-mock:8080")
