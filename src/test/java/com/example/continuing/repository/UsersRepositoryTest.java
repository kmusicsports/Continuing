package com.example.continuing.repository;

import com.example.continuing.common.Utils;
import com.example.continuing.entity.Users;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.sql.Timestamp;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsersRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UsersRepository usersRepository;

    private Users testUser1;
    private Users testUser2;

    private static final String TEST_NAME = "testName";
    private static final String TEST_EMAIl = "test@user.email";

    @BeforeEach
    void setUp() {
        Timestamp timestampNow = Utils.timestampNow();

        testUser1 = new Users();
        testUser1.setName(TEST_NAME);
        testUser1.setEmail("test@user1.email");
        testUser1.setPassword("testUser1Pass");
        testUser1.setProfileMessage("usersRepositoryTestProfileMessage");
        testUser1.setContinuousDays(15);
        testUser1.setLanguage("ja");
        testUser1.setCreatedAt(timestampNow);
        testUser1.setUpdatedAt(timestampNow);

        testUser2 = new Users();
        testUser2.setName("usersRepositoryTestName");
        testUser2.setEmail(TEST_EMAIl);
        testUser2.setPassword("testUser2Pass");
        testUser2.setProfileMessage("user2TestProfileMessage");
        testUser2.setContinuousDays(30);
        testUser2.setLanguage("en");
        testUser2.setCreatedAt(timestampNow);
        testUser2.setUpdatedAt(timestampNow);

        entityManager.persist(testUser1);
        entityManager.persist(testUser2);
    }

    @AfterEach
    void tearDown() {
        entityManager.remove(testUser1);
        entityManager.remove(testUser2);
    }

    @Test
    void testFindByName() {
        Users actual = usersRepository.findByName(TEST_NAME).get();
        assertThat(actual).isEqualTo(testUser1);
    }

    @Test
    void testFindByEmail() {
        Users actual = usersRepository.findByEmail(TEST_EMAIl).get();
        assertThat(actual).isEqualTo(testUser2);
    }

    @Test
    void testFindByNameContainingIgnoreCase() {
        List<Users> actualList = usersRepository.findByNameContainingIgnoreCase("REPOSITORY");
        assertThat(actualList.get(0)).isEqualTo(testUser2);
        assertThat(actualList.size()).isEqualTo(1);
    }

    @Test
    void testFindByProfileMessageContainingIgnoreCase() {
        List<Users> actualList = usersRepository.findByProfileMessageContainingIgnoreCase("REPOSITORY");
        assertThat(actualList.get(0)).isEqualTo(testUser1);
        assertThat(actualList.size()).isEqualTo(1);
    }

}