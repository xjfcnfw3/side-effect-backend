package sideeffect.project.domain.comment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.StringUtils;
import sideeffect.project.common.domain.BaseTimeEntity;
import sideeffect.project.domain.freeboard.FreeBoard;
import sideeffect.project.domain.user.User;

@Getter
@Entity
@Setter
@ToString
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

    @Id
    @Column(name = "comment_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "free_board_id")
    private FreeBoard freeBoard;

    public Comment(String content) {
        this.content = content;
    }

    public void update(String content) {
        if (StringUtils.hasText(content)) {
            this.content = content;
        }
    }

    public void associate(User user, FreeBoard freeBoard) {
        freeBoard.addComment(this);
        user.addComment(this);
        this.freeBoard = freeBoard;
        this.user = user;
    }
}
