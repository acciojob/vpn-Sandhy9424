package com.driver.services.impl;

import com.driver.model.Country;
import com.driver.model.CountryName;
import com.driver.model.ServiceProvider;
import com.driver.model.User;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository3;
    @Autowired
    ServiceProviderRepository serviceProviderRepository3;
    @Autowired
    CountryRepository countryRepository3;

    @Override
    public User register(String username, String password, String countryName) throws Exception{
        User user=new User();
        user.setUsername(username);
        user.setPassword(password);
        if(countryName.equalsIgnoreCase("IND") || countryName.equalsIgnoreCase("USA") || countryName.equalsIgnoreCase("AUS") || countryName.equalsIgnoreCase("CHI") || countryName.equalsIgnoreCase("JPN") ) {
             Country country=new Country();
            if (countryName.equalsIgnoreCase("IND")) {
                country.setCountryName(CountryName.IND);
                country.setCode(CountryName.IND.toCode());
            } else if (countryName.equalsIgnoreCase("USA")) {
                country.setCountryName(CountryName.USA);
                country.setCode(CountryName.USA.toCode());
            } else if (countryName.equalsIgnoreCase("AUS")) {
                country.setCountryName(CountryName.AUS);
                country.setCode(CountryName.AUS.toCode());
            } else if (countryName.equalsIgnoreCase("CHI")) {
                country.setCountryName(CountryName.CHI);
                country.setCode(CountryName.CHI.toCode());
            } else if (countryName.equalsIgnoreCase("JPN")) {
                country.setCountryName(CountryName.JPN);
                country.setCode(CountryName.JPN.toCode());
            }
            country.setUser(user);
            user.setOriginalCountry(country);
            user.setOriginalIp(country.getCode()+"."+user.getId());
            user.setMaskedIp(null);
            user.setConnected(false);
             userRepository3.save(user);
        }
        else{
            throw new Exception("Country not found");
        }
        return user;
     }

    @Override
    public User subscribe(Integer userId, Integer serviceProviderId) {
        User user=userRepository3.findById(userId).get();
        ServiceProvider serviceProvider=serviceProviderRepository3.findById(serviceProviderId).get();
        List<ServiceProvider>serviceProviderList=user.getServiceProviderList();
        serviceProviderList.add(serviceProvider);
        user.setServiceProviderList(serviceProviderList);
        List<User>userList=serviceProvider.getUsers();
        userList.add(user);
        userRepository3.save(user);
        return user;
    }
}
