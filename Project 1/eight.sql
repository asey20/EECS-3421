select o.title, o.year, o.price
from yrb_offer o
where o.price > 
(select avg(max)
from (select o.club, max(o.price) max
	from yrb_offer o, yrb_book b
	where b.title = o.title AND b.year = o.year AND b.language = 'English'
	group by o.club))
;

