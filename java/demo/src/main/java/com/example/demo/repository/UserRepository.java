/*
package com.example.demo.repository;
import com.example.demo.pojo.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends CrudRepository<User,Integer> {

}
*/

package com.example.demo.repository;
import com.example.demo.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUserName(String username);
    User findByEmail(String email);
    User save(User user);
    List<User> findByUserNameContaining(String keyword);
    List<User> findByEmailContaining(String keyword);
    //Optional<User> findByUserName(String username);

}