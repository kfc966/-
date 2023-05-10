package com.example.blogapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blogapi.dao.mapper.CommentMapper;
import com.example.blogapi.dao.pojo.Comment;
import com.example.blogapi.dao.pojo.SysUser;
import com.example.blogapi.service.CommentService;
import com.example.blogapi.service.SysUserService;
import com.example.blogapi.utils.UserThreadLocal;
import com.example.blogapi.vo.CommentVo;
import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.UserVo;
import com.example.blogapi.vo.params.CommentParam;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private SysUserService  sysUserService;
    @Override
    public Result commentsByArticleId(Long id) {
            LambdaQueryWrapper<Comment> commentQueryWrapper=new LambdaQueryWrapper<>();
            commentQueryWrapper.eq(Comment::getArticleId,id);
            commentQueryWrapper.eq(Comment::getLevel,1);
        List<Comment> commentList = commentMapper.selectList(commentQueryWrapper);
        List<CommentVo> commentVos=copyList(commentList);
        return Result.success(commentVos);

    }

    private List<CommentVo> copyList(List<Comment> commentList) {
        List<CommentVo> commentVoList=new ArrayList<>();
        for(Comment comment:commentList)
        {
            commentVoList.add(copy(comment));
        }
        return   commentVoList;
    }

    private CommentVo copy(Comment comment) {
        CommentVo commentVo=new CommentVo();
        BeanUtils.copyProperties(comment,commentVo);
        commentVo.setId(String.valueOf(comment.getId()));
        commentVo.setCreateDate(String.valueOf(comment.getCreateDate()));
        //作者信息
        Long articleId = comment.getAuthorId();
        UserVo userVo = sysUserService.findUserVoById(articleId);
        commentVo.setAuthor(userVo);
        if(comment.getLevel()==1)
        {
            Long id = comment.getId();
            //跟据上级评论id查找子评论
            List<CommentVo> commentVoList=findCommentsByParentId(id);
            commentVo.setChildrens(commentVoList);
        }
        //给其他人回的评论
        if(comment.getLevel()>1)
        {
            Long toUid = comment.getToUid();
            UserVo touserVo = sysUserService.findUserVoById(toUid);
            commentVo.setToUser(touserVo);

        }
        return  commentVo;

    }
    @Override
     public List<CommentVo> findCommentsByParentId(Long id) {
        LambdaQueryWrapper<Comment> commentWrapper=new LambdaQueryWrapper<>();
        commentWrapper.eq(Comment::getParentId,id);
        return  copyList(commentMapper.selectList(commentWrapper));
    }
    //评论
    @Override
    public Result comment(CommentParam commentParam) {
        SysUser sysUser = UserThreadLocal.get();
        Comment comment = new Comment();
        //1485180162242678785
        comment.setArticleId(commentParam.getArticleId());
        comment.setAuthorId(sysUser.getId());
        comment.setContent(commentParam.getContent());
        comment.setCreateDate(System.currentTimeMillis());
        Long parent = commentParam.getParent();
        if (parent == null || parent == 0) {
            comment.setLevel(1);
        }else{
            comment.setLevel(2);
        }
        comment.setParentId(parent == null ? 0 : parent);
        Long toUserId = commentParam.getToUserId();
        comment.setToUid(toUserId == null ? 0 : toUserId);

        this.commentMapper.insert(comment);
        return Result.success(null);
    }
}
