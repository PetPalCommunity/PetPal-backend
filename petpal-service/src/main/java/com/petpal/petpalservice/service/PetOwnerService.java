package com.petpal.petpalservice.service;


import com.petpal.petpalservice.exception.DuplicateResourceException;
import com.petpal.petpalservice.exception.InvalidEmailFormatException;
import com.petpal.petpalservice.exception.MissingRequiredFieldException;
import com.petpal.petpalservice.exception.InvalidCredentialsException;
import com.petpal.petpalservice.exception.UserNotFoundException;
import com.petpal.petpalservice.model.dto.PetOwnerRequestDto;
import com.petpal.petpalservice.model.dto.SignInRequestDto;
import com.petpal.petpalservice.model.entity.PetOwner;
import com.petpal.petpalservice.repository.PetOwnerRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class PetOwnerService {
    private final PetOwnerRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;

    public PetOwnerService(PetOwnerRepository repository) {
        this.repository = repository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public PetOwner createPetOwner(PetOwnerRequestDto dto) {
        if (dto.getOwnerName() == null || dto.getOwnerName().isEmpty() ||
                dto.getOwnerSex() == null || dto.getOwnerSex().isEmpty() ||
                dto.getOwnerAge() <= 0 ||
                dto.getOwnerPhone() <= 0 || (int) Math.log10(dto.getOwnerPhone()) + 1 != 9) {
            throw new MissingRequiredFieldException("Required field is missing");
        }

        if (dto.getOwnerEmail() == null || !dto.getOwnerEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new InvalidEmailFormatException("Invalid email format");
        }

        if (repository.existsByOwnerEmail(dto.getOwnerEmail()) ||
                repository.existsByOwnerPhone(dto.getOwnerPhone())) {
            throw new DuplicateResourceException("Resource already exists");
        }

        PetOwner petOwner = new PetOwner();
        petOwner.setOwnerName(dto.getOwnerName());
        petOwner.setOwnerSex(dto.getOwnerSex());
        petOwner.setOwnerAge(dto.getOwnerAge());
        petOwner.setOwnerEmail(dto.getOwnerEmail());
        petOwner.setOwnerPhone(dto.getOwnerPhone());
        petOwner.setOwnerPassword(passwordEncoder.encode(dto.getOwnerPassword()));

        return repository.save(petOwner);
    }

    public PetOwner validateSignIn(SignInRequestDto dto) {
        PetOwner petOwner = repository.findByOwnerEmail(dto.getOwnerEmail());

        if (petOwner == null || !passwordEncoder.matches(dto.getOwnerPassword(), petOwner.getOwnerPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        return petOwner;
    }

    public void incrementFollowersCount(int id) {
        PetOwner petOwner = getPetOwner(id);
        petOwner.setOwnerFollowed(petOwner.getOwnerFollowed() + 1);
        repository.save(petOwner);
    }

    public void decrementFollowersCount(int id) {
        PetOwner petOwner = getPetOwner(id);
        petOwner.setOwnerFollowed(petOwner.getOwnerFollowed() - 1);
        repository.save(petOwner);
    }

    public void incrementFollowedCount(int id) {
        PetOwner petOwner = getPetOwner(id);
        petOwner.setOwnerFollowers(petOwner.getOwnerFollowers() + 1);
        repository.save(petOwner);
    }

    public void decrementFollowedCount(int id) {
        PetOwner petOwner = getPetOwner(id);
        petOwner.setOwnerFollowers(petOwner.getOwnerFollowers() - 1);
        repository.save(petOwner);
    }

    public PetOwner getPetOwner(int id) {
        return repository.findById(id).orElseThrow(() -> new UserNotFoundException("PetOwner with id " + id + " not found"));
    }

    public List<PetOwner> getAllPetOwners() {
        return repository.findAll();
    }
}