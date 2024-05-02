import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        OnlineLibraryProxy onlineLibrary = OnlineLibraryProxy.getInstance();
        while (true) {
            String command = sc.nextLine();
            if (Objects.equals(command, "end")) {
                break;
            }
            String[] words = command.split(" ");
            switch (words[0]) {
                case "createBook" -> {
                    String title = words[1];
                    String author = words[2];
                    String price = words[3];
                    onlineLibrary.createBook(title, author, price);
                }
                case "createUser" -> {
                    String type = words[1];
                    String username = words[2];
                    onlineLibrary.createUser(username, type);
                }
                case "subscribe" -> {
                    String username = words[1];
                    onlineLibrary.subscribe(username);
                }
                case "unsubscribe" -> {
                    String username = words[1];
                    onlineLibrary.unsubscribe(username);
                }
                case "updatePrice" -> {
                    String title = words[1];
                    String new_price = words[2];
                    onlineLibrary.updatePrice(title, new_price);
                }
                case "readBook" -> {
                    String username = words[1];
                    String title = words[2];
                    onlineLibrary.readBook(title, username);
                }
                case "listenBook" -> {
                    String username = words[1];
                    String title = words[2];
                    onlineLibrary.listenBook(title, username);
                }
            }
        }
    }
}

abstract class LibraryInterface {
    abstract void createUser(String name, String type);

    abstract void createBook(String name, String author, String price);

    abstract void readBook(String title, String username);

    abstract void listenBook(String title, String username);

}

class OnlineLibrary extends LibraryInterface implements Subject {
    public static OnlineLibrary instance;
    private HashMap<String, User> users = new HashMap<>();
    private HashMap<String, Book> books = new HashMap<>();
    private ArrayList<User> subscribedUsers = new ArrayList<>();
    private final StandardUserFactory standardUserFactory = new StandardUserFactory();
    private final PremiumUserFactory premiumUserFactory = new PremiumUserFactory();

    private OnlineLibrary() {

    }

    public static OnlineLibrary getInstance() {
        if (instance == null) {
            instance = new OnlineLibrary();
        }
        return instance;
    }

    @Override
    public void createUser(String name, String type) {
        if (users.containsKey(name)) {
            System.out.println("User already exists");
            return;
        }
        UserFactoryClient userFactoryClient;
        switch (type) {
            case "standard" -> {
                userFactoryClient = new UserFactoryClient(standardUserFactory);
                users.put(name, userFactoryClient.createUser(name));
            }
            case "premium" -> {
                userFactoryClient = new UserFactoryClient(premiumUserFactory);
                users.put(name, userFactoryClient.createUser(name));
            }
        }
    }

    @Override
    public void createBook(String title, String author, String price) {
        if (books.containsKey(title)) {
            System.out.println("Book already exists");
            return;
        }
        books.put(title, new Book(title, author, price));
    }

    @Override
    void readBook(String title, String username) {
        books.get(title).readBook(username);
    }

    @Override
    void listenBook(String title, String username) {
        if (users.get(username).isPremium()) {
            books.get(title).listenBook(username);
        } else {
            System.out.println("No access");
        }
    }

    @Override
    public void subscribe(String name) {
        if (users.get(name).isSubscribed()) {
            System.out.println("User already subscribed");
            return;
        }
        subscribedUsers.add(users.get(name));
        users.get(name).setSubscribed(true);
    }

    @Override
    public void unsubscribe(String name) {
        if (!users.get(name).isSubscribed()) {
            System.out.println("User is not subscribed");
            return;
        }
        subscribedUsers.remove(users.get(name));
        users.get(name).setSubscribed(false);
    }

    @Override
    public void updatePrice(String bookTitle, String price) {
        books.get(bookTitle).setPrice(price);
        notifyUsers(bookTitle, price);
    }

    @Override
    public void notifyUsers(String bookTitle, String price) {
        for (User user : subscribedUsers) {
            user.update(bookTitle, price);
        }
    }
}

class OnlineLibraryProxy extends LibraryInterface implements Subject {
    private static OnlineLibraryProxy instance;
    private static final OnlineLibrary onlineLibrary = OnlineLibrary.getInstance();

    private OnlineLibraryProxy() {
    }

    public static OnlineLibraryProxy getInstance() {
        if (instance == null) {
            instance = new OnlineLibraryProxy();
        }
        return instance;
    }


    @Override
    void createUser(String name, String type) {
        onlineLibrary.createUser(name, type);
    }

    @Override
    void createBook(String name, String author, String price) {
        onlineLibrary.createBook(name, author, price);
    }

    @Override
    void readBook(String title, String username) {
        onlineLibrary.readBook(title, username);
    }

    @Override
    void listenBook(String title, String username) {
        onlineLibrary.listenBook(title, username);
    }

    @Override
    public void subscribe(String name) {
        onlineLibrary.subscribe(name);
    }

    @Override
    public void unsubscribe(String name) {
        onlineLibrary.unsubscribe(name);
    }

    @Override
    public void updatePrice(String bookTitle, String price) {
        onlineLibrary.updatePrice(bookTitle, price);
    }

    @Override
    public void notifyUsers(String bookTitle, String price) {
        System.out.println("YOU'RE NOT SUPPOSED TO CALL ME");
    }
}

interface Observer {
    void update(String bookTitle, String price);
}

interface Subject {
    void subscribe(String name);

    void unsubscribe(String name);

    void updatePrice(String bookTitle, String price);

    void notifyUsers(String bookTitle, String price);
}

class User implements Observer {
    private final String name;
    private final String type;
    private boolean isSubscribed = false;

    public User(String name, String type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public void update(String bookTitle, String price) {
        System.out.println(name + " notified about price update for " + bookTitle + " to " + price);
    }

    public boolean isPremium() {
        return Objects.equals(type, "premium");
    }

    public void setSubscribed(boolean subscribed) {
        isSubscribed = subscribed;
    }

    public boolean isSubscribed() {
        return isSubscribed;
    }
}

interface UserFactory {
    User createUser(String name);
}

class StandardUserFactory implements UserFactory {

    @Override
    public User createUser(String name) {
        return new User(name, "standard");
    }
}

class PremiumUserFactory implements UserFactory {

    @Override
    public User createUser(String name) {
        return new User(name, "premium");
    }
}

class UserFactoryClient {
    UserFactory factory;

    UserFactoryClient(UserFactory factory) {
        this.factory = factory;
    }

    public User createUser(String name) {
        return factory.createUser(name);
    }
}

class Book {
    private final String title;
    private final String author;
    private String price;

    public Book(String title, String author, String price) {
        this.title = title;
        this.author = author;
        this.price = price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void readBook(String userName) {
        System.out.println(userName + " reading " + title + " by " + author);
    }

    public void listenBook(String userName) {
        System.out.println(userName + " listening " + title + " by " + author);
    }
}