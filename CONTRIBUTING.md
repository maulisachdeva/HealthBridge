# Contributing to HealthBridge

Thank you for contributing to HealthBridge – Smart Clinic Management and Queue Automation System.

All team members must follow this workflow to avoid conflicts and keep the project organized.

## 1. Project Modules

| Module | Responsibility |
|---|---|
| Module 1 | Patient & Doctor Management |
| Module 2 | Appointment & Smart Queue Management |
| Module 3 | Medical Records & Prescription Management |
| Module 4 | Billing, Payments & Reports |

## 2. Clone the Repository

```bash
git clone https://github.com/maulisachdeva/HealthBridge.git
cd HealthBridge
```

## 3. Get the Latest Development Code

Always start from the latest `dev` branch.

```bash
git checkout dev
git pull origin dev
```

## 4. Create Your Feature Branch

Each member must work on their own feature branch.

Examples:

```bash
git checkout -b feature/member2/appointment-queue
```

```bash
git checkout -b feature/member3/medical-records
```

```bash
git checkout -b feature/member4/billing-reports
```

Do not directly modify or push code to `dev` or `main`.

## 5. Save Your Work

After completing a task:

```bash
git status
git add .
git commit -m "Describe your changes"
git push origin YOUR_BRANCH_NAME
```

Use meaningful commit messages.

Examples:
- Add appointment booking functionality
- Implement medical record management
- Add billing and payment operations

## 6. Create a Pull Request

1. Open the HealthBridge repository on GitHub.
2. Select Compare & pull request.
3. Set the base branch to `dev`.
4. Set the compare branch to your feature branch.
5. Add a clear title and description.
6. Review the changed files.
7. Submit the Pull Request.

Do not merge into `main`.

## 7. Keep Your Branch Updated

Before starting new work, synchronize with `dev`:

```bash
git checkout dev
git pull origin dev
```

Then update your feature branch:

```bash
git checkout YOUR_BRANCH_NAME
git merge dev
```

Resolve any conflicts carefully before pushing again.

## 8. Important Rules

- Do not push directly to `main` or `dev`.
- Do not modify another member's module without coordination.
- Do not commit database passwords, API keys, or personal information.
- Do not commit unnecessary IDE or build files.
- Test your code before creating a Pull Request.
- Coordinate shared classes, database tables, and integration changes with the team leader.

## 9. Project Integration

The `dev` branch is the integration branch for all four modules.

The `main` branch will contain the final stable version of HealthBridge.

Happy coding!
