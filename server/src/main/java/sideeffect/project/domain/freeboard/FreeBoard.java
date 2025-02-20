package sideeffect.project.domain.freeboard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import sideeffect.project.common.domain.BaseTimeEntity;
import sideeffect.project.domain.comment.Comment;
import sideeffect.project.domain.like.Like;
import sideeffect.project.domain.user.User;

@Entity
@Getter
@Table(
    name = "free_boards",
    indexes = {@Index(name = "user_index", columnList = "user_id")},
    uniqueConstraints = {
        @UniqueConstraint(
            name = "unique_project_url",
            columnNames = "project_url"
        )
    }
)
@SQLDelete(sql = "UPDATE free_boards SET deleted=true WHERE free_board_id=?")
@Where(clause = "deleted = false")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FreeBoard extends BaseTimeEntity {

    @Id
    @Column(name = "free_board_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int views;

    private String title;

    @Column(name = "project_url")
    private String projectUrl;

    private String content;

    private String imgUrl;

    private String projectName;

    private String subTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "freeBoard", orphanRemoval = true,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @OrderBy("id desc")
    private List<Comment> comments;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "freeBoard", orphanRemoval = true,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Like> likes;

    private boolean deleted;

    @Builder
    public FreeBoard(Long id, String title, String projectUrl, String content, String imgUrl, String projectName,
        String subTitle) {
        this.id = id;
        this.views = 0;
        this.title = title;
        this.projectUrl = projectUrl;
        this.content = content;
        this.imgUrl = imgUrl;
        this.projectName = projectName;
        this.comments = new ArrayList<>();
        this.likes = new HashSet<>();
        this.subTitle = subTitle;
    }

    public void update(FreeBoard freeBoard) {
        if (freeBoard.getContent() != null) {
            this.content = freeBoard.getContent();
        }
        if (freeBoard.getTitle() != null) {
            this.title = freeBoard.getTitle();
        }
        if (freeBoard.getProjectUrl() != null) {
            this.projectUrl = freeBoard.getProjectUrl();
        }
        if (freeBoard.getProjectName() != null) {
            this.projectName = freeBoard.getProjectName();
        }
        if (freeBoard.getSubTitle() != null) {
            this.subTitle = freeBoard.getSubTitle();
        }
    }

    public void increaseViews() {
        this.views++;
    }

    public void changeImageUrl(String url) {
        this.imgUrl = url;
    }

    public void deleteImageUrl() {
        this.imgUrl = null;
    }

    public void associateUser(User user) {
        if (this.user != null) {
            this.user.deleteFreeBoard(this);
        }
        user.addFreeBoard(this);
        this.user = user;
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }

    public void deleteComment(Comment comment) {
        this.comments.remove(comment);
    }

    public void addLike(Like like) {
        this.likes.add(like);
    }

    public void deleteLike(Like like) {
        this.likes.remove(like);
    }
}
