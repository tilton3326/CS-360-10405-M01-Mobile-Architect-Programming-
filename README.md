# CS-360-10405-M01-Mobile-Architect-Programming-

**Briefly summarize the requirements and goals of the app you developed. What user needs was this app designed to address?**

The goal of the project was to create an inventory management application that allows users to securely log in and manage inventory data. The app provides a real-time, accurate list of all inventory items, enabling users to monitor stock levels effectively. It also supports CRUD (Create, Read, Update, Delete) operations so users can add new items, view details, modify existing entries, or remove items as needed. Additionally, the app requests SMS permissions to send low inventory alerts and push notifications, ensuring users stay informed about critical stock levels. This design addresses the need for efficient inventory oversight and timely notifications for users managing stock.

**What screens and features were necessary to support user needs and produce a user-centered UI for the app? How did your UI designs keep users in mind? Why were your designs successful?**

The essential screens for supporting user needs included the login screen, the main inventory display, and the notification center. To prioritize user experience, each screen features clear labeling with titles to help users understand their current context. The inventory list includes column headers to clearly identify data points, making it easier to scan and interpret information quickly. I focused on simplicity by avoiding clutter and choosing intuitive icons, which even non-technical users can recognize and navigate effortlessly. These design choices proved successful because they created an accessible, straightforward interface that effectively meets user expectations without unnecessary complexity.

**How did you approach the process of coding your app? What techniques or strategies did you use? How could those techniques or strategies be applied in the future?**

My development approach involved dividing the project into manageable phases, separating layout design from functionality implementation. Initially, I studied similar applications to inform my layout and user flow decisions. After establishing the visual structure, I built each screen step-by-step, then integrated the functional code to enable interactions. This modular approach allowed me to test and troubleshoot each component individually, reducing errors and improving efficiency. In the future, such a strategy can be applied to break complex projects into smaller, testable parts, streamlining development and debugging processes.

**How did you test to ensure your code was functional? Why is this process important, and what did it reveal?**

I tested my code incrementally by validating each feature as I developed it. For example, I populated the database with dummy data to verify that CRUD functions operated correctly before implementing notification features. This step-by-step testing was vital because it helped me identify and isolate issues early, making debugging more manageable. It also ensured the overall stability of the app, revealing potential edge cases and bugs that could have compromised user experience if left unaddressed.

**Consider the full app design and development process from initial planning to finalization. Where did you have to innovate to overcome a challenge?**

A significant challenge arose when attempting to delete multiple items or randomly ordered items, which caused the app to crash. This was due to errors with list indexing. To resolve this, I innovated by refactoring how the RecyclerView was updated, utilizing LiveData to automatically synchronize the displayed list with the database. This approach replaced manual list modifications, preventing out-of-bounds errors and ensuring stable deletion functionality, which was critical for app reliability.

**In what specific component of your mobile app were you particularly successful in demonstrating your knowledge, skills, and experience?**

I demonstrated particular strength during the planning phase. Despite being new to full-stack development, I conducted thorough research and created a detailed plan for the app’s structure and features. Following this plan closely during coding meant I required minimal adjustments later, which led to a smoother development process and a cohesive final product. This experience underscored the importance of careful planning and preparation, showcasing my ability to translate theoretical knowledge into practical implementation.
