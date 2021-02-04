package mirror42.dev.cinemates.ui.post;

import mirror42.dev.cinemates.model.User;

public interface ReactionListener {

    void onCommentDeleted();
    void onCommentPosted();

    void onPostCommentButtonClicked(String commentText, long postID, User loggedUser);
}
