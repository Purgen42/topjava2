package ru.javaops.lunch_vote.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaops.lunch_vote.model.User;
import ru.javaops.lunch_vote.repository.UserRepository;
import ru.javaops.lunch_vote.util.UserUtil;

import java.util.List;
import java.util.Optional;

import static ru.javaops.lunch_vote.util.validation.ValidationUtil.*;


@Service
@AllArgsConstructor
@Slf4j
@CacheConfig(cacheNames = "users")
public class UserService {
    private final UserRepository repository;

    public User safeGet(int id) {
        return checkOptional(get(id));
    }

    public Optional<User> get(int id) {
        log.info("get {}", id);
        return repository.findById(id);
    }

    public Optional<User> getByEmail(String email) {
        log.info("getByEmail {}", email);
        return repository.getByEmail(email);
    }

    @CacheEvict(value = "users", allEntries = true)
    public User create(User user) {
        log.info("create {}", user);
        checkNew(user);
        Assert.notNull(user, "user must not be null");
        return repository.save(UserUtil.prepareToSave(user));
    }

    @CacheEvict(value = "users", allEntries = true)
    public void update(User user, int id) {
        log.info("update {} with id={}", user, id);
        assureIdConsistent(user, id);
        Assert.notNull(user, "user must not be null");
        repository.save(UserUtil.prepareToSave(user));
    }

    @Cacheable("users")
    public List<User> getAll() {
        log.info("getAll");
        return repository.findAll(Sort.by(Sort.Direction.ASC, "name", "email"));
    }

    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public void enable(int id, boolean enabled) {
        log.info(enabled ? "enable {}" : "disable {}", id);
        User user = safeGet(id);
        user.setEnabled(enabled);
    }

    @CacheEvict(value = "users", allEntries = true)
    public void delete(int id) {
        log.info("delete {}", id);
        repository.deleteExisted(id);
    }
}
