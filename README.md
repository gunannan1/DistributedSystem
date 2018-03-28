#### How to initialise this project on your own computer
1. Ensure your computer installed git tool
> For windows, install [here](https://gitforwindows.org) if not

> For Mac/Linux, your computer may already has, just try command `git`

2. Generate your ssh key if you konw command well( Mac or Linux)
> check [here](https://confluence.atlassian.com/bitbucketserver/creating-ssh-keys-776639788.html)

3. Add your public key into our project
```shell
    cat ~/.ssh/id_rsa.pub
```

   - Open web : [https://kangnwh.visualstudio.com/_details/security/keys](https://kangnwh.visualstudio.com/_details/security/keys)
   - Add the content in your `id_rsa.pub`
   
   
4. Clone code to your own computer
```shell
    cd <your_project_folder>
    git clone ssh://kangnwh@vs-ssh.visualstudio.com:22/_ssh/DistributedSystem
```

5. Open this folder in your IDE and you can use your IDE to sync code of your PC with remote code.

#### workplace 
Open [this](https://kangnwh.visualstudio.com/DistributedSystem/_dashboards) for our project dashboard
