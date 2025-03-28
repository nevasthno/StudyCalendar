import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Config {
    @Value("${db.password}")
    private String password;

    public String getPassword() {
        return password;
    }
}
