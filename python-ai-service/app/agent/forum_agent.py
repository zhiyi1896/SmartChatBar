from langchain_openai import ChatOpenAI
from langchain.agents import create_tool_calling_agent, AgentExecutor
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_core.runnables.history import RunnableWithMessageHistory

from app.tools.forum_tools import get_hot_posts, summarize_post, check_content, search_posts, get_user_profile
from app.memory.chat_memory import get_session_history, get_session_lock, trim_session_history
from app.config import settings


class ForumAgent:
    def __init__(self):
        self.llm = ChatOpenAI(
            model=settings.deepseek_model,
            temperature=settings.temperature,
            max_tokens=settings.max_tokens,
            openai_api_key=settings.deepseek_api_key,
            openai_api_base=settings.deepseek_base_url,
        )

        self.tools = [get_hot_posts, summarize_post, check_content, search_posts, get_user_profile]
        self.prompt = ChatPromptTemplate.from_messages([
            ("system", "You are a forum AI assistant. Help users inspect hot posts, summarize posts, review content, search posts, and view user profiles. Prefer concise helpful answers."),
            MessagesPlaceholder(variable_name="history"),
            ("user", "{input}"),
            MessagesPlaceholder(variable_name="agent_scratchpad"),
        ])

        agent = create_tool_calling_agent(self.llm, self.tools, self.prompt)
        self.agent_executor = AgentExecutor(agent=agent, tools=self.tools, verbose=settings.debug)
        self.agent_with_history = RunnableWithMessageHistory(
            self.agent_executor,
            get_session_history,
            input_messages_key="input",
            history_messages_key="history",
        )

    async def ask(self, query: str, user_id: str) -> dict:
        config = {"configurable": {"session_id": user_id}}
        lock = get_session_lock(user_id)
        async with lock:
            result = await self.agent_with_history.ainvoke({"input": query}, config=config)
            trim_session_history(user_id, settings.max_history * 2)
        return {"answer": result["output"], "session_id": user_id}
