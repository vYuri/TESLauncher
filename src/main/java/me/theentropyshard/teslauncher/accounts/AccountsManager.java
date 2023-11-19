/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023 TESLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package me.theentropyshard.teslauncher.accounts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.gui.playview.PlayViewHeader;
import me.theentropyshard.teslauncher.utils.PathUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountsManager {
    private final Path accountsFile;
    private final Map<String, Account> accounts;
    private final Gson gson;

    public AccountsManager(Path workDir) {
        this.accountsFile = workDir.resolve("accounts.json");

        try {
            PathUtils.createFileIfNotExists(this.accountsFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.accounts = new HashMap<>();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Account.class, new AccountDeserializer())
                .disableHtmlEscaping()
                .create();
    }

    public static Account getCurrentAccount() {
        Map<String, Account> accountsMap = TESLauncher.getInstance().getAccountsManager().getAccountsMap();
        return accountsMap.get(String.valueOf(PlayViewHeader.instance.getAccounts().getSelectedItem()));
    }

    public void loadAccounts() throws IOException {
        InputStream inputStream = Files.newInputStream(this.accountsFile);
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

        Map<String, Account> accounts = this.gson.fromJson(reader, new TypeToken<Map<String, Account>>() {}.getType());
        if (accounts != null) {
            this.accounts.putAll(accounts);
        }
    }

    public boolean saveAccount(Account account) {
        if (this.accounts.containsKey(account.getUsername())) {
            return false;
        }

        this.accounts.put(account.getUsername(), account);

        try {
            this.save();

            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteAccount(String nickname) {
        if (this.accounts.containsKey(nickname)) {
            this.accounts.remove(nickname);

            try {
                this.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return true;
        }

        return false;
    }

    public void save() throws IOException {
        String json = this.gson.toJson(this.accounts);
        Files.write(this.accountsFile, json.getBytes(StandardCharsets.UTF_8));
    }

    public List<Account> getAccounts() {
        return new ArrayList<>(this.accounts.values());
    }

    public Map<String, Account> getAccountsMap() {
        return this.accounts;
    }
}
