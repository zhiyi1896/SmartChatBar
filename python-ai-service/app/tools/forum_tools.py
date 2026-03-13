import httpx
from typing import Optional, Dict
from langchain_core.tools import tool
from app.config import settings


async def call_java_api(endpoint: str, params: Optional[Dict] = None) -> Dict:
    async with httpx.AsyncClient() as client:
        url = f"{settings.java_api_base_url}/{endpoint.lstrip('/')}"
        response = await client.get(url, params=params, timeout=10.0)
        response.raise_for_status()
        return response.json()


@tool
async def get_hot_posts(days: int = 7, limit: int = 10) -> str:
    """获取最近几天的热门帖子"""
    try:
        data = await call_java_api("/hot/weekly", {"days": days, "limit": limit})
        posts = data.get("data", {}).get("list", [])
        if not posts:
            return "暂无热帖数据"
        result = "热门帖子列表\n\n"
        for index, post in enumerate(posts, start=1):
            result += f"{index}. {post.get('title')}\n"
            result += f"   作者：{post.get('authorName')} 点赞：{post.get('likeCount')} 评论：{post.get('commentCount')}\n"
        return result
    except Exception as exc:
        return f"获取热帖失败：{str(exc)}"


@tool
async def summarize_post(post_id: int) -> str:
    """总结指定帖子的核心内容"""
    try:
        data = await call_java_api(f"/post/detail/{post_id}")
        post = data.get("data", {})
        if not post:
            return f"未找到ID为 {post_id} 的帖子"
        title = post.get("title", "无标题")
        content = post.get("content", "")
        author = post.get("authorName", "匿名")
        return f"需要总结的帖子信息：\n标题：{title}\n作者：{author}\n内容：{content}\n\n请用简洁的语言总结这个帖子的核心观点。"
    except Exception as exc:
        return f"获取帖子失败：{str(exc)}"


@tool
async def check_content(text: str) -> str:
    """检查文本内容是否包含敏感或违规信息"""
    try:
        async with httpx.AsyncClient() as client:
            response = await client.get(
                f"{settings.java_api_base_url}/sensitive/check",
                params={"text": text},
                timeout=5.0,
            )
            data = response.json()
            has_sensitive = data.get("data", False)
        return "内容包含敏感词，请修改后重试" if has_sensitive else "内容审核通过，可以发布"
    except Exception as exc:
        return f"内容审核失败：{str(exc)}"


@tool
async def search_posts(keyword: str) -> str:
    """按关键词搜索论坛帖子"""
    try:
        data = await call_java_api("/search", {"keyword": keyword})
        posts = data.get("data", {}).get("list", [])
        if not posts:
            return f"没有找到和 {keyword} 相关的帖子"
        result = f"和 {keyword} 相关的帖子有：\n"
        for post in posts[:5]:
            result += f"- {post.get('title')}（作者：{post.get('authorName')}）\n"
        return result
    except Exception as exc:
        return f"搜索失败：{str(exc)}"


@tool
async def get_user_profile(user_id: int) -> str:
    """获取指定用户的主页信息"""
    try:
        data = await call_java_api(f"/profile/{user_id}")
        profile = data.get("data", {})
        if not profile:
            return f"未找到用户 {user_id}"
        return (
            f"用户昵称：{profile.get('nickname')}\n"
            f"角色：{profile.get('role')}\n"
            f"帖子数：{profile.get('postCount')}\n"
            f"粉丝数：{profile.get('followerCount')}\n"
            f"关注数：{profile.get('followingCount')}\n"
            f"简介：{profile.get('bio') or '暂无简介'}"
        )
    except Exception as exc:
        return f"获取用户主页失败：{str(exc)}"
