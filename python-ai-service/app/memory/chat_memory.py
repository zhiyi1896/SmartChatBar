from collections import OrderedDict
import asyncio
import threading
from typing import Dict
from langchain_core.chat_history import BaseChatMessageHistory
from langchain_community.chat_message_histories import ChatMessageHistory

MAX_SESSIONS = 1000
store: "OrderedDict[str, ChatMessageHistory]" = OrderedDict()
session_locks: Dict[str, asyncio.Lock] = {}
store_lock = threading.RLock()


def get_session_history(session_id: str) -> BaseChatMessageHistory:
    with store_lock:
        history = store.get(session_id)
        if history is None:
            history = ChatMessageHistory()
            store[session_id] = history
            while len(store) > MAX_SESSIONS:
                expired_session_id, _ = store.popitem(last=False)
                session_locks.pop(expired_session_id, None)
        else:
            store.move_to_end(session_id)
        return history


def get_session_lock(session_id: str) -> asyncio.Lock:
    with store_lock:
        lock = session_locks.get(session_id)
        if lock is None:
            lock = asyncio.Lock()
            session_locks[session_id] = lock
        return lock


def trim_session_history(session_id: str, max_messages: int):
    if max_messages <= 0:
        return
    with store_lock:
        history = store.get(session_id)
        if history is None:
            return
        if len(history.messages) > max_messages:
            history.messages[:] = history.messages[-max_messages:]
        store.move_to_end(session_id)


def clear_session_history(session_id: str):
    with store_lock:
        store.pop(session_id, None)
        session_locks.pop(session_id, None)
