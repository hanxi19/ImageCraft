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

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUserName(String username);
    User save(User user);
}