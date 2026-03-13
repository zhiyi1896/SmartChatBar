from pydantic_settings import BaseSettings
from pydantic import ConfigDict


class Settings(BaseSettings):
    app_name: str = "论坛AI助手"
    debug: bool = False
    java_api_base_url: str = "http://localhost:8080/api"
    deepseek_api_key: str = ""
    deepseek_base_url: str = "https://api.deepseek.com/v1"
    deepseek_model: str = "deepseek-chat"
    temperature: float = 0.7
    max_tokens: int = 2000
    max_history: int = 10

    model_config = ConfigDict(env_file=".env", env_file_encoding="utf-8")


settings = Settings()
