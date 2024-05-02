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
    /**
     * Method for creating users.
     *
     * @param name Username
     * @param type Standard/Premium
     */
    abstract void createUser(String name, String type);

    /**
     * Method for creating books.
     *
     * @param name   Book title
     * @param author Author of the book
     * @param price  Price of the book (is String, due to assignment requirements (easier to work with))
     */
    abstract void createBook(String name, String author, String price);

    /**
     * Method for reading a book.
     *
     * @param title    Title of the book to read
     * @param username User who will be reading a book
     */
    abstract void readBook(String title, String username);

    /**
     * Method for listening to audio version of the book (Premium feature).
     *
     * @param title    Title of the book to listen
     * @param username User who will listen to a book
     */
    abstract void listenBook(String title, String username);

    /**
     * Method for adding a book into "to read" list.
     *
     * @param book     Book to add
     * @param username User book will be added to
     */
    abstract void addBookToReadingList(String book, String username);

    /**
     * Method for removing a book from the "to read" list.
     *
     * @param book     Book to remove
     * @param username User from whom book will be removed
     */
    abstract void removeBookFromReadingList(String book, String username);

    /**
     * Method to show whole "to read" list.
     *
     * @param name User whose list to show
     */
    abstract void displayToReadList(String name);

    /**
     * Method for reading a whole list.
     *
     * @param username User to read the list
     */
    abstract void readToReadList(String username);

    /**
     * Method for adding a book into playlist (Premium feature).
     *
     * @param book     Book to add
     * @param username User book will be added to
     */
    abstract void addBookToPlaylist(String book, String username);

    /**
     * Method for removing a book from the playlist (Premium feature).
     *
     * @param book     Book to remove
     * @param username User from whom book will be removed
     */
    abstract void removeBookFromPlaylist(String book, String username);

    /**
     * Method to show whole playlist (Premium feature).
     *
     * @param name User whose list to show
     */
    abstract void displayPlaylist(String name);

    /**
     * Method for listening to a playlist (Premium feature).
     *
     * @param username User who will listen to the playlist
     */
    abstract void listenPlaylist(String username);
}

/**
 * Singleton online library class, which is at the same time a subject that subscribed users observe
 */
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
    void addBookToReadingList(String book, String username) {
        users.get(username).addBookToRead(books.get(book));
    }

    @Override
    void removeBookFromReadingList(String book, String username) {
        users.get(username).removeBookFromReadingList(books.get(book));
    }

    @Override
    void displayToReadList(String name) {
        users.get(name).seeToReadList();
    }

    @Override
    void readToReadList(String username) {
        users.get(username).readList();
    }

    @Override
    void addBookToPlaylist(String book, String username) {
        if (users.get(username).isPremium()) {
            users.get(username).addBookToListen(books.get(book));
        } else {
            System.out.println("No access");
        }
    }

    @Override
    void removeBookFromPlaylist(String book, String username) {
        if (users.get(username).isPremium()) {
            users.get(username).removeBookFromPlaylist(books.get(book));
        } else {
            System.out.println("No access");
        }
    }

    @Override
    void displayPlaylist(String name) {
        if (users.get(name).isPremium()) {
            users.get(name).seePlaylist();
        } else {
            System.out.println("No access");
        }
    }

    @Override
    void listenPlaylist(String username) {
        if (users.get(username).isPremium()) {
            users.get(username).listenPlaylist();
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

/**
 * Proxy class for reaching to OnlineLibrary class, cutting unnecessary code
 */
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
    void addBookToReadingList(String book, String username) {
        onlineLibrary.addBookToReadingList(book, username);
    }

    @Override
    void removeBookFromReadingList(String book, String username) {
        onlineLibrary.removeBookFromReadingList(book, username);
    }

    @Override
    void displayToReadList(String name) {
        onlineLibrary.displayToReadList(name);
    }

    @Override
    void readToReadList(String username) {
        onlineLibrary.readToReadList(username);
    }

    @Override
    void addBookToPlaylist(String book, String username) {
        onlineLibrary.addBookToPlaylist(book, username);
    }

    @Override
    void removeBookFromPlaylist(String book, String username) {
        onlineLibrary.removeBookFromPlaylist(book, username);
    }

    @Override
    void displayPlaylist(String name) {
        onlineLibrary.displayPlaylist(name);
    }

    @Override
    void listenPlaylist(String username) {
        onlineLibrary.listenPlaylist(username);
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

/**
 * Interface for observers on update of OnlineLibrary
 */
interface Observer {
    /**
     * Print notification to users
     *
     * @param bookTitle Book that was changed
     * @param price     New price
     */
    void update(String bookTitle, String price);
}

/**
 * Interface for the online library that would change and notify every observer
 */
interface Subject {
    /**
     * Subscribe user on updates
     *
     * @param name Username
     */
    void subscribe(String name);

    /**
     * Unsubscribe users from updates
     *
     * @param name Username
     */
    void unsubscribe(String name);

    /**
     * Update prices of a book
     *
     * @param bookTitle Title of the book
     * @param price     New price
     */
    void updatePrice(String bookTitle, String price);

    /**
     * Notify users on the update
     *
     * @param bookTitle Title of the book
     * @param price     New price
     */
    void notifyUsers(String bookTitle, String price);
}

/**
 * Main User class, that is the observer on changes
 */
class User implements Observer {
    private final String name;
    private final String type;
    private boolean isSubscribed = false;
    private BookToReadList toReadList = new BookToReadList();
    private BooksToListenPlaylist playlist = new BooksToListenPlaylist();

    public User(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public void addBookToRead(BookReadingManager book) {
        toReadList.addBook(book);
    }

    public void removeBookFromReadingList(BookReadingManager book) {
        toReadList.removeBook(book);
    }

    public void seeToReadList() {
        toReadList.display();
    }

    public void readList() {
        toReadList.readBook(name);
    }

    public void addBookToListen(BookReadingManager book) {
        playlist.addBook(book);
    }

    public void removeBookFromPlaylist(BookReadingManager book) {
        playlist.removeBook(book);
    }

    public void seePlaylist() {
        playlist.display();
    }

    public void listenPlaylist() {
        playlist.readBook(name);
    }

    @Override
    public void update(String bookTitle, String price) {
        System.out.println(name + " notified about price update for " + bookTitle + " to " + price);
    }

    /**
     * Check if user is premium
     *
     * @return true/false
     */
    public boolean isPremium() {
        return Objects.equals(type, "premium");
    }

    /**
     * Set status of subscription on updates
     *
     * @param subscribed true/false
     */
    public void setSubscribed(boolean subscribed) {
        isSubscribed = subscribed;
    }

    public boolean isSubscribed() {
        return isSubscribed;
    }
}

/**
 * Interface for user factories
 */
interface UserFactory {
    /**
     * Creates user depending on which factory it's in
     *
     * @param name Name of the user
     * @return New user
     */
    User createUser(String name);
}

/**
 * Standard type user factory
 */
class StandardUserFactory implements UserFactory {

    @Override
    public User createUser(String name) {
        return new User(name, "standard");
    }
}

/**
 * Premium type user factory
 */
class PremiumUserFactory implements UserFactory {

    @Override
    public User createUser(String name) {
        return new User(name, "premium");
    }
}

/**
 * Client for creating users depending on a factory passed
 */
class UserFactoryClient {
    UserFactory factory;

    UserFactoryClient(UserFactory factory) {
        this.factory = factory;
    }

    /**
     * Call for a factory in "factory field"
     *
     * @param name Username
     * @return New user
     */
    public User createUser(String name) {
        return factory.createUser(name);
    }
}

/**
 * Main book class
 */
class Book implements BookReadingManager {
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

    @Override
    public void readBook(String userName) {
        System.out.println(userName + " reading " + title + " by " + author);
    }

    @Override
    public void listenBook(String userName) {
        System.out.println(userName + " listening " + title + " by " + author);
    }
}

/**
 * Interface for reading books and managing book lists and playlists
 * (additional feature, is not used in the task (except for single books))
 */
interface BookReadingManager {
    /**
     * Method for reading a book
     *
     * @param userName Name of a user who reads
     */
    void readBook(String userName);

    /**
     * Method for listening a book (premium feature)
     *
     * @param userName Name of a user who listens to a book
     */
    void listenBook(String userName);
}

/**
 * List of books to read, which all will be read one after another (additional feature, is not used in the task)
 */
class BookToReadList implements BookReadingManager {
    private ArrayList<BookReadingManager> toReadList = new ArrayList<>();

    public BookToReadList() {
    }

    @Override
    public void readBook(String userName) {
        for (BookReadingManager book : toReadList) {
            book.readBook(userName);
        }
    }

    @Override
    public void listenBook(String userName) {

    }

    public void addBook(BookReadingManager book) {
        if (toReadList.contains(book)) {
            System.out.println("Book already in the list");
            return;
        }
        toReadList.add(book);
    }

    public void removeBook(BookReadingManager book) {
        if (!toReadList.contains(book)) {
            System.out.println("Book is not in the list");
            return;
        }
        toReadList.remove(book);
    }

    public void display() {
        System.out.println(toReadList);
    }
}

/**
 * List of books to listen, which all will be played one after another (additional feature, is not used in the task)
 */
class BooksToListenPlaylist implements BookReadingManager {
    private ArrayList<BookReadingManager> playlist = new ArrayList<>();

    public BooksToListenPlaylist() {
    }

    @Override
    public void readBook(String userName) {

    }

    @Override
    public void listenBook(String userName) {
        for (BookReadingManager book : playlist) {
            book.readBook(userName);
        }
    }

    public void addBook(BookReadingManager book) {
        if (playlist.contains(book)) {
            System.out.println("Book already in the list");
            return;
        }
        playlist.add(book);
    }

    public void removeBook(BookReadingManager book) {
        if (!playlist.contains(book)) {
            System.out.println("Book is not in the list");
            return;
        }
        playlist.remove(book);
    }

    public void display() {
        System.out.println(playlist);
    }
}