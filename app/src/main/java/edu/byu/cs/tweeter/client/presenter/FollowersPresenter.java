package edu.byu.cs.tweeter.client.presenter;


import java.util.List;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.FollowService;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class FollowersPresenter{

    private final User user;
    private final AuthToken authToken;
    private final View view;

    private static final int PAGE_SIZE = 10;

    private FollowService followService;

    private User lastFollower;
    private boolean hasMorePages;
    private boolean isLoading = false;



    public boolean isHasMorePages() {
        return hasMorePages;
    }

    public void setHasMorePages(boolean hasMorePages) {
        this.hasMorePages = hasMorePages;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    /**
     * The interface by which this presenter communicates with it's view.
     */
    public interface View {
        void displayErrorMessage(String message);
        void setLoadingStatus(boolean value);
        void addFollowers(List<User> followers);

    }


    public FollowersPresenter(View view) {
        this.view = view;
        this.user = new User();
        this.authToken = new AuthToken();
        followService = new FollowService();
    }

    public void loadMoreItems(User user) {

        if (!isLoading) {   // This guard is important for avoiding a race condition in the scrolling code.
            isLoading = true;
            view.setLoadingStatus(true);
        }

        followService.getFollowers(Cache.getInstance().getCurrUserAuthToken(),
                user, PAGE_SIZE, lastFollower, new GetFollowersObserver());


    }


    public class GetFollowersObserver implements FollowService.GetFollowersObserver
    {

        @Override
        public void handleSuccess(List<User> followers, boolean hasMorePages) {
            isLoading = false;
            view.setLoadingStatus(false);

            lastFollower = (followers.size() > 0) ? followers.get(followers.size() - 1) : null;

            setHasMorePages(hasMorePages);

            view.addFollowers(followers);

        }

        @Override
        public void handleFailure(String message) {
            isLoading = false;
            view.setLoadingStatus(false);

            String finalMessage = "Failed to get followers: " + message;
            view.displayErrorMessage(finalMessage);
        }

        @Override
        public void handleException(Exception exception) {
            isLoading = false;
            view.setLoadingStatus(false);

            String finalMessage = "Failed to get followers because of exception: " + exception.getMessage();
            view.displayErrorMessage(finalMessage);
        }
    }
}
