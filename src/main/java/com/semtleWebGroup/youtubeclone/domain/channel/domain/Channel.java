package com.semtleWebGroup.youtubeclone.domain.channel.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.semtleWebGroup.youtubeclone.domain.auth.domain.Member;
import com.semtleWebGroup.youtubeclone.domain.comment.domain.Comment;
import com.semtleWebGroup.youtubeclone.domain.video.domain.Video;
import lombok.*;

import javax.persistence.*;
import java.sql.Blob;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "channel")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Channel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "channel_id", updatable = false)
    private Long id;

    @Column(nullable = false, length = 15, unique = true)
    private String title;
    @Column(length = 70)
    private String description;

    @ManyToMany
    // 자기 참조로 M:N관계
    @JoinTable(name = "subscription",
            joinColumns = @JoinColumn(name = "channel_id"), // 엔티티와 매핑될 외래키 지정
            inverseJoinColumns = @JoinColumn(name = "subscriber_id")    // 매핑될 다른 엔티티의 외래키 지정
    )
    // 구독 채널은 중복이 될 수 없으므로 set 사용
    private Set<Channel> subscribedChannels = new HashSet<>();

    // 구독자 수를 찾기 위해
    @ManyToMany(mappedBy = "subscribedChannels")
    private Set<Channel> subscribers = new HashSet<>();

    @Lob
    private Blob channelImage;

    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Video> videos = new HashSet<>();
    @OneToMany(mappedBy = "channel", orphanRemoval = true)
    private Set<Video> videoLikeLists = new HashSet<>();
    @OneToMany(mappedBy = "channel", cascade = CascadeType.REMOVE)
    private Set<Comment> comments = new HashSet<>();
    @OneToMany(mappedBy = "channel", cascade = CascadeType.REMOVE)
    private Set<Comment> commentsLikeLists = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public Channel(String title, String description, Member member){
        this.member = member;
        this.title = title;
        this.description = description;
    }

    public void setChannelImage(Blob imageFile) {
        this.channelImage = imageFile;
    }



    public void update(String title, String description){
        if (title != null) this.title=title;
        if (description != null) this.description=description;
    }

    public void setSubscribedChannels(Set<Channel> subscribedChannels) {
        this.subscribedChannels = subscribedChannels;
    }

    public void setSubscribers(Set<Channel> subscribers) {
        this.subscribers = subscribers;
    }

    // channel에서 video를 추가하는 메소드
    public void addVideo(Video video) {
        videos.add(video);
        video.setChannel(this);
    }

    // channel에서 video를 삭제하는 메소드
    public void removeVideo(Video video) {
        videos.remove(video);
    }

    public void likeVideo(Video video){
        videoLikeLists.add(video);
        video.addLikeChannel(this);
    }

    public void unLikeVideo(Video video){
        videoLikeLists.remove(video);
        video.removeLikeChannel(this);
    }

    public void addComment(Comment comment){
        comments.add(comment);
        comment.setChannel(this);
    }

    public void deleteComment(Comment comment) {
        for (Comment replyComment: comment.getReplyComments()) {   //부모는 아니지만 명시적인 처리를 위해서
            comments.remove(replyComment);
        }
        comments.remove(comment);
        //comment.setChannel(null);
    }

    public void likeComment(Comment comment) {
        commentsLikeLists.add(comment);
        comment.addLike(this);
    }

    public void unLikeComment(Comment comment) {
        commentsLikeLists.remove(comment);
        comment.removeLike(this);
    }
}
