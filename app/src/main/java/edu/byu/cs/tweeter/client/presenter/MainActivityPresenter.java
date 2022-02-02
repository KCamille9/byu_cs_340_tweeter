package edu.byu.cs.tweeter.client.presenter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import edu.byu.cs.tweeter.R;
import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.FollowService;
import edu.byu.cs.tweeter.client.model.service.StatusService;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;

public class MainActivityPresenter {

    private FollowService followService;
    private UserService userService;
    private StatusService statusService;


    private View view;




    public interface View {

        void displayErrorMessage(String message);

        void logoutUserPresenter();

        void updateFollowerCount(int count);

        void updateFolloweeCount(int count);

        void setIsFollowerCase();

        void setIsNotFollowerCase();

        void followObserverSuccess();

        void enableFollowButton();

        void unFollowObserverSuccess();

        void postStatusSuccess();
    }

    public MainActivityPresenter(View view) {
        this.view = view;
        followService = new FollowService();
        userService = new UserService();
    }

    public String getFormattedDateTime() throws ParseException {
        SimpleDateFormat userFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        SimpleDateFormat statusFormat = new SimpleDateFormat("MMM d yyyy h:mm aaa");

        return statusFormat.format(userFormat.parse(LocalDate.now().toString() + " " + LocalTime.now().toString().substring(0, 8)));
    }

    public List<String> parseURLs(String post) {
        List<String> containedUrls = new ArrayList<>();
        for (String word : post.split("\\s")) {
            if (word.startsWith("http://") || word.startsWith("https://")) {

                int index = findUrlEndIndex(word);

                word = word.substring(0, index);

                containedUrls.add(word);
            }
        }

        return containedUrls;
    }

    public List<String> parseMentions(String post) {
        List<String> containedMentions = new ArrayList<>();

        for (String word : post.split("\\s")) {
            if (word.startsWith("@")) {
                word = word.replaceAll("[^a-zA-Z0-9]", "");
                word = "@".concat(word);

                containedMentions.add(word);
            }
        }

        return containedMentions;
    }

    public int findUrlEndIndex(String word) {
        if (word.contains(".com")) {
            int index = word.indexOf(".com");
            index += 4;
            return index;
        } else if (word.contains(".org")) {
            int index = word.indexOf(".org");
            index += 4;
            return index;
        } else if (word.contains(".edu")) {
            int index = word.indexOf(".edu");
            index += 4;
            return index;
        } else if (word.contains(".net")) {
            int index = word.indexOf(".net");
            index += 4;
            return index;
        } else if (word.contains(".mil")) {
            int index = word.indexOf(".mil");
            index += 4;
            return index;
        } else {
            return word.length();
        }
    }

    public void executeFollowTask(User user) {
        followService.GetFollowTask(Cache.getInstance().getCurrUserAuthToken(), user, new MainActivityPresenter.FollowObserver());
    }

    public void executeUnfollowTask(User user) {
        followService.GetUnFollowTask(Cache.getInstance().getCurrUserAuthToken(), user, new MainActivityPresenter.UnFollowObserver());
    }

    public void executeIsFollowerTask(User user) {
        followService.GetIsFollowerTask(Cache.getInstance().getCurrUserAuthToken(),
                Cache.getInstance().getCurrUser(), user, new MainActivityPresenter.IsFollowerObserver());

    }

    public void executeLogOutTask() {
        userService.GetLogOutTask(Cache.getInstance().getCurrUserAuthToken(), new MainActivityPresenter.LogOutObserver());
    }

    public void executePostStatusTask(String post) throws ParseException {

        Status newStatus = new Status(post, Cache.getInstance().getCurrUser(),
                getFormattedDateTime(), parseURLs(post), parseMentions(post));

        statusService.GetPostStatusTask(Cache.getInstance().getCurrUserAuthToken(), newStatus,
                new MainActivityPresenter.PostStatusObserver());
    }

    public void executeGetFollowersCountTask(User user) {

        followService.GetGetFollowersCountTask(Cache.getInstance().getCurrUserAuthToken(), user,
                new MainActivityPresenter.GetFollowersCountObserver());
    }

    public void executeGetFollowingCountTask(User user) {
        followService.GetGetFollowingCountTask(Cache.getInstance().getCurrUserAuthToken(),
                user, new MainActivityPresenter.GetFollowingCountObserver());
    }



    private class FollowObserver implements FollowService.FollowObserver {
        @Override
        public void handleSuccess() {
            view.followObserverSuccess();
            view.enableFollowButton();
        }

        @Override
        public void handleFailure(String message) {
            view.displayErrorMessage("Failed to follow: "  + message);
            view.enableFollowButton();
        }

        @Override
        public void handleException(Exception exception) {
            view.displayErrorMessage("Failed to follow because of exception: " + exception.getMessage());
            view.enableFollowButton();
        }
    }

    private class UnFollowObserver implements FollowService.UnFollowObserver {

        @Override
        public void handleSuccess() {
            view.unFollowObserverSuccess();
            view.enableFollowButton();
        }

        @Override
        public void handleFailure(String message) {
            view.displayErrorMessage("Failed to follow: "  + message);
            view.enableFollowButton();
        }

        @Override
        public void handleException(Exception exception) {
            view.displayErrorMessage("Failed to follow because of exception: " + exception.getMessage());
            view.enableFollowButton();
        }
    }

    private class IsFollowerObserver implements FollowService.IsFollowerObserver {

        @Override
        public void handleSuccess(boolean isFollower) {
            // If logged in user if a follower of the selected user, display the follow button as "following"
            if (isFollower) {
                view.setIsFollowerCase();
            } else {
                view.setIsNotFollowerCase();
            }
        }

        @Override
        public void handleFailure(String message) {
            view.displayErrorMessage("Failed to determine following relationship: "  + message);
        }

        @Override
        public void handleException(Exception exception) {
            view.displayErrorMessage("Failed to determine following relationship because of exception: " + exception.getMessage());
        }
    }

    private class LogOutObserver implements UserService.LogOutObserver {

        @Override
        public void handleSuccess() {
            view.logoutUserPresenter();
        }

        @Override
        public void handleFailure(String message) {
            view.displayErrorMessage("Failed to logout: " + message);
        }

        @Override
        public void handleException(Exception exception) {
            view.displayErrorMessage("Failed to logout because of exception: " + exception.getMessage());
        }
    }

    private class PostStatusObserver implements StatusService.PostStatusObserver {

        @Override
        public void handleSuccess() {
            view.postStatusSuccess();
        }

        @Override
        public void handleFailure(String message) {
            view.displayErrorMessage("Failed to post status: " + message);
        }

        @Override
        public void handleException(Exception exception) {
            view.displayErrorMessage("Failed to post status because of exception: " + exception.getMessage());
        }
    }

    private class GetFollowersCountObserver implements FollowService.GetFollowersCountObserver {

        @Override
        public void handleSuccess(int count) {
            view.updateFollowerCount(count);
        }

        @Override
        public void handleFailure(String message) {
            view.displayErrorMessage("Failed to get followers count: " + message);
        }

        @Override
        public void handleException(Exception exception) {
            view.displayErrorMessage("Failed to get followers count because of exception: " + exception.getMessage());

        }
    }

    private class GetFollowingCountObserver implements FollowService.GetFollowingCountObserver {

        @Override
        public void handleSuccess(int count) {
            view.updateFolloweeCount(count);
        }

        @Override
        public void handleFailure(String message) {
            view.displayErrorMessage("Failed to get following count: " + message);
        }

        @Override
        public void handleException(Exception exception) {
            view.displayErrorMessage("Failed to get following count because of exception: " + exception.getMessage());

        }
    }
}
